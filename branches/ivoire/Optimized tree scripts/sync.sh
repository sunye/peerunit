#!/bin/bash


#scp -r ~/  frontend.sophia.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.rennes.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.lille.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.orsay.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.nancy.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.bordeaux.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.grenoble.grid5000.fr:/home/rennes/akoita/
#scp -r ~/  frontend.lyon.grid5000.fr:/home/rennes/akoita/

rsync --delete -avuz   ~/ akoita@frontend.sophia.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.lille.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.orsay.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.nancy.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.bordeaux.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.grenoble.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.lyon.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.toulouse.grid5000.fr:
rsync --delete -avuz   ~/ akoita@frontend.rennes.grid5000.fr:
