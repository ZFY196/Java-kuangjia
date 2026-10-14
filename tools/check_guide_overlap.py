"""Compare project source with the code blocks of lab one, tasks 1 through 4."""
import argparse
import hashlib
import json
import re
from pathlib import Path
from zipfile import ZipFile
import xml.etree.ElementTree as ET

WORD_NS = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
LEXEME = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|[\w$]+|[^\w\s]', re.UNICODE)
COMMENTS = re.compile(r'<!--.*?-->|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*', re.S)
JAVA_WORDS = set('abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while true false null'.split())


def guide_blocks(path):
    with ZipFile(path) as archive:
        root = ET.fromstring(archive.read('word/document.xml'))
    capturing = False
    current = []
    blocks = []
    for paragraph in root.findall('.//w:body//w:p', WORD_NS):
        text = ''.join(node.text or '' for node in paragraph.findall('.//w:t', WORD_NS))
        if re.match(r'任务\s*1\s*开发环境搭建', text):
            capturing = True
        if capturing and re.match(r'任务\s*5\s*', text):
            break
        if not capturing:
            continue
        shade = paragraph.find('w:pPr/w:shd', WORD_NS)
        is_code = shade is not None and shade.get('{%s}fill' % WORD_NS['w']) == 'F2F2F2'
        if is_code:
            current.append(text)
        elif current:
            blocks.append('\n'.join(current))
            current = []
    if current:
        blocks.append('\n'.join(current))
    if not blocks:
        raise ValueError('No code blocks found in lab one tasks 1-4')
    return blocks


def tokens(text, suffix, structural=False):
    if suffix == '.sql':
        text = re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|--[^\n]*',
                      lambda match: match.group(0) if match.group(0).startswith(('"', "'")) else '', text)
    text = COMMENTS.sub(lambda match: match.group(0) if match.group(0).startswith(('"', "'")) else '', text)
    if suffix == '.properties':
        text = re.sub(r'^\s*[#!].*$', '', text, flags=re.M)
    result = LEXEME.findall(text)
    if structural and suffix == '.java':
        result = ['STR' if item.startswith(('"', "'")) else
                  'NUM' if item[0].isdigit() else
                  'ID' if re.match(r'^[\w$]+$', item) and item not in JAVA_WORDS else item
                  for item in result]
    return result


def coverage(candidate, reference_windows, width):
    covered = set()
    for start in range(max(0, len(candidate) - width + 1)):
        if tuple(candidate[start:start + width]) in reference_windows:
            covered.update(range(start, start + width))
    return len(covered)


def run(root, guide, width):
    blocks = guide_blocks(guide)
    exact_windows = set()
    java_structure_windows = set()
    for block in blocks:
        suffix = '.java' if re.search(r'\b(package|QueryWrapper|Page<|public class)\b', block) else '.properties' if 'log4j.' in block else '.sql' if 'CREATE DATABASE' in block else '.xml'
        sequence = tokens(block, suffix)
        exact_windows.update(tuple(sequence[i:i + width]) for i in range(max(0, len(sequence) - width + 1)))
        if suffix == '.java':
            sequence = tokens(block, suffix, True)
            java_structure_windows.update(tuple(sequence[i:i + width]) for i in range(max(0, len(sequence) - width + 1)))
    files = sorted([root / 'pom.xml'] + list((root / 'src').rglob('*.java')) +
                   list((root / 'src').rglob('*.xml')) + list((root / 'src').rglob('*.properties')) +
                   list((root / 'sql').rglob('*.sql')))
    rows = []
    for path in files:
        sequence = tokens(path.read_text(encoding='utf-8-sig'), path.suffix)
        matched = coverage(sequence, exact_windows, width)
        structural = coverage(tokens(path.read_text(encoding='utf-8-sig'), '.java', True),
                              java_structure_windows, width) if path.suffix == '.java' else None
        rows.append({'file': path.relative_to(root).as_posix(), 'tokens': len(sequence),
                     'matched_tokens': matched, 'exact_percent': round(100 * matched / max(1, len(sequence)), 2),
                     'java_structural_matched_tokens': structural})
    groups = {}
    selections = {
        'all_source': rows,
        'production_without_tests': [row for row in rows if not row['file'].startswith('src/test/')],
        'java_production': [row for row in rows if row['file'].startswith('src/main/java/')],
        'java_tests': [row for row in rows if row['file'].startswith('src/test/java/')],
    }
    for name, items in selections.items():
        count = sum(row['tokens'] for row in items)
        matched = sum(row['matched_tokens'] for row in items)
        groups[name] = {'tokens': count, 'matched_tokens': matched,
                        'exact_percent': round(100 * matched / max(1, count), 2)}
        java_items = [row for row in items if row['java_structural_matched_tokens'] is not None]
        java_total = sum(row['tokens'] for row in java_items)
        if java_total:
            groups[name]['java_structural_percent'] = round(
                100 * sum(row['java_structural_matched_tokens'] for row in java_items) / java_total, 2)
    return {'reference': str(guide.resolve()), 'reference_sha256': hashlib.sha256(guide.read_bytes()).hexdigest(),
            'reference_code_blocks': len(blocks), 'window_tokens': width,
            'method': 'Continuous token window coverage; comments and whitespace removed. Java structural comparison also normalizes identifiers and literals. No library configuration is excluded.',
            'groups': groups, 'files': rows}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--guide', type=Path, required=True)
    parser.add_argument('--root', type=Path, default=Path.cwd())
    parser.add_argument('--window', type=int, default=25)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    if args.window < 2:
        parser.error('--window must be at least 2')
    report = run(args.root.resolve(), args.guide, args.window)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(report['groups'], ensure_ascii=False, indent=2))
    for row in report['files']:
        if row['exact_percent'] >= 30:
            print(row['file'], row['exact_percent'])


if __name__ == '__main__':
    main()
