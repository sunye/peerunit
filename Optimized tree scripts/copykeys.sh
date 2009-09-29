#!/bin/bash

	cat ~/.ssh/id_*.pub | ssh akoita@frontend.sophia.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.lille.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.orsay.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.nancy.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.bordeaux.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.grenoble.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.lyon.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.rennes.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
	cat ~/.ssh/id_*.pub | ssh akoita@frontend.toulouse.grid5000.fr 'umask 022 ; mkdir -p .ssh ; touch .ssh/authorized_keys ; cat >> .ssh/authorized_keys'
