lines = []
for line in open('langprofiles.txt'):
  lines.append(line.strip())

for i in xrange(0, len(lines), 3):
  print '''classifier.add(new Classifier("%s", "%s", "%s"));''' % (' '.join(lines[i].split()[1:]), lines[i+2], lines[i+1])