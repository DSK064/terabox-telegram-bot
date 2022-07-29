#!/usr/bin/env python

import json
import logging
from sys import argv

if len(argv) < 2:
  print "Usage:"
  print "  %s manifest.json_path name container [-d]" % argv[0]
  exit(2)

if "-d" in argv:
  logging.basicConfig(level=logging.DEBUG)
  argv.remove("-d")

mfile = argv[1]
deployable_name = argv[2]
container = argv[3]

manifest = json.load(open(mfile))

for deployable in manifest["deploy"]:
  if deployable["name"] == deployable_name:
    deployable["container"] = container

logging.debug(json.dumps(manifest, indent=2))
json.dump(manifest, open(mfile, "w"), indent=2)
