import re

# Fix AppScanServiceImpl.java : replace hardcoded 5.0 in manifest blocks
impl_path = r'backend\src\main\java\com\example\backend\scan\service\AppScanServiceImpl.java'

with open(impl_path, 'rb') as f:
    raw = f.read()

# Work with \r\n (Windows line endings)
content = raw.decode('utf-8')

old = 'new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, 5.0, title, mobileScan);'
count_before = content.count(old)
print(f"[AppScanServiceImpl] Occurrences avant remplacement : {count_before}")

# Use re.DOTALL and match \r\n explicitly
pattern = re.compile(
    r'([ \t]+String title = issue\.getEffectiveTitle\(\)[^\r\n]*\r\n)'   # title line
    r'([ \t]*\r\n)'                                                        # blank line
    r'([ \t]+String fullDesc = desc \+ " \(Manifest\)";\r\n'              # fullDesc block
    r'[ \t]+if \(fullDesc\.length\(\) > 255\) \{\r\n'
    r'[ \t]+fullDesc = fullDesc\.substring\(0, 250\) \+ "\.\.\.";\r\n'
    r'[ \t]+\}\r\n'
    r'[ \t]*\r\n)'                                                         # blank line
    r'([ \t]+Vulnerabilite v = new Vulnerabilite\(null, cwe, fullDesc, sevEnum, cwe, 5\.0, title, mobileScan\);)',
    re.MULTILINE
)

def replacer(m):
    title_line  = m.group(1)
    blank1      = m.group(2)
    full_block  = m.group(3)
    vuln_line   = m.group(4)
    # Derive indentation from vuln_line
    indent_str  = vuln_line[:len(vuln_line) - len(vuln_line.lstrip())]
    cvss_line   = indent_str + 'double cvss = issue.getEffectiveCvss(); // real CVSS from MobSF\r\n'
    new_vuln    = vuln_line.replace('5.0', 'cvss')
    return title_line + cvss_line + blank1 + full_block + new_vuln

new_content, n = pattern.subn(replacer, content)
print(f"[AppScanServiceImpl] Remplacements effectues : {n}")

with open(impl_path, 'wb') as f:
    f.write(new_content.encode('utf-8'))

print(f"[AppScanServiceImpl] Fichier sauvegarde.")

# Verify
remaining = new_content.count(old)
print(f"[AppScanServiceImpl] Occurrences restantes apres remplacement : {remaining}")

print("\nDone. Toutes les corrections ont ete appliquees.")
