import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path

RESULT_PATH = Path("target/checkstyle-result.xml")

if not RESULT_PATH.exists():
    raise SystemExit("target/checkstyle-result.xml not found. Run mvnw checkstyle:check first.")

by_file = defaultdict(list)
root = ET.parse(RESULT_PATH).getroot()
for file_elem in root.findall("file"):
    file_name = file_elem.get("name", "")
    errors = file_elem.findall("error")
    if not errors:
        continue
    for err in errors:
        by_file[file_name].append({
            "line": err.get("line"),
            "column": err.get("column"),
            "message": err.get("message"),
            "source": err.get("source"),
        })

print(f"Total files with violations: {len(by_file)}")
print()

by_package = defaultdict(int)
for path, errors in by_file.items():
    # crude package inference from path components
    parts = Path(path).parts
    try:
        idx = parts.index("com")
        package = ".".join(parts[idx:-1])
    except ValueError:
        package = "misc"
    by_package[package] += len(errors)

print("Violation count by package (descending):")
for package, count in sorted(by_package.items(), key=lambda x: x[1], reverse=True):
    print(f"  {package}: {count}")

print()

print("Top offenders:")
for path, errors in sorted(by_file.items(), key=lambda x: len(x[1]), reverse=True)[:20]:
    print(f"  {path}: {len(errors)}")

