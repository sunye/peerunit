package fr.inria.peerunit.onstree.testerTree;

public class TesterTree {
	private TesterNode_be root = null;

	// La m�thode de r��quilibrage suivante est appel�e apr�s un ajout dans le
	// sous-arbre droit qui a provoqu� une augmentation de la hauteur du sous
	// arbre droit :
	private boolean equilibreD(TesterNode_be r, TesterNode_be p, boolean g) {
		// r est le fils gauche de p si g vaut true, r est le fils droit de p si
		// g vaut false
		// retourne true si apr�s �quilibrage l'arbre a grandi
		TesterNode_be r1, r2;
		switch (r.equilibre) {
		case -1:
			r.equilibre = 0;
			return false;
		case 0:
			r.equilibre = 1;
			return true;
		case 1:
		default:
			r1 = r.childR;

			if (r1.equilibre == 1) {
				r.childR = r1.childL;
				r1.childL = r;
				r.equilibre = 0;
				r = r1;
			} else {
				r2 = r1.childL;
				r1.childL = r2.childR;
				r2.childR = r1;
				r.childR = r2.childL;
				r2.childL = r;
				if (r2.equilibre == 1)
					r.equilibre = -1;
				else
					r.equilibre = 0;
				if (r2.equilibre == -1)
					r1.equilibre = 1;
				else
					r1.equilibre = 0;
				r = r2;
			}
			// refaire le cha�nage avec le p�re
			if (p == null)
				root = r;
			else if (g)
				p.childL = r;
			else
				p.childR = r;
			r.equilibre = 0;
			return false;
		}
	}

	// La m�thode de r��quilibrage suivante est appel�e apr�s un ajout dans le
	// sous-arbre childL qui a provoqu� une augmentation de la hauteur du sous
	// arbre childL :
	private boolean equilibreG(TesterNode_be r, TesterNode_be p, boolean g) {
		// r est le fils childL de p si g vaut true, r est le fils childR de p
		// si g vaut false
		// retourne true si apr�s �quilibrage l'arbre a grandi
		TesterNode_be r1, r2;
		switch (r.equilibre) {
		case 1:
			r.equilibre = 0;
			return false;
		case 0:
			r.equilibre = -1;
			return true;
		case -1:
		default:
			r1 = r.childL;
			if (r1.equilibre == -1) {
				r.childL = r1.childR;
				r1.childR = r;
				r.equilibre = 0;
				r = r1;
			} else {
				r2 = r1.childR;
				r1.childR = r2.childL;
				r2.childL = r1;
				r.childL = r2.childR;
				r2.childR = r;
				if (r2.equilibre == -1)
					r.equilibre = 1;
				else
					r.equilibre = 0;
				if (r2.equilibre == 1)
					r1.equilibre = -1;
				else
					r1.equilibre = 0;
				r = r2;
			}
			// refaire le cha�nage avec le p�re
			if (p == null)
				root = r;
			else if (g)
				p.childL = r;
			else
				p.childR = r;
			r.equilibre = 0;
			return false;
		}
	}

	// La fonction d'ajout est écrite alors de la façon suivante :
	public void add(int x) {
		ajoutAVL(root, null, true, x);
	}

	/**
	 * 
	 * @param r
	 * @param p
	 * @param g
	 * @param e
	 * @return
	 */
	private boolean ajoutAVL(TesterNode_be r, TesterNode_be p, boolean g, int e) {
		if (r == null) // construction du sous arbre de testeurs d'une station
		{
			r = new TesterNode_be(e, null, null);
			if (p == null)
				root = r;
			else if (g)
				p.childL = r;
			else
				p.childR = r;
			return true;
		} else {
			int a = compareTo(e, r.id);
			if (a == 0) {
				return false; // a déjà présent dans l'arbre
			}
			if (a < 0) {
				if (ajoutAVL(r.childL, r, true, e)) {
					return equilibreG(r, p, g);

				} else {
					return false;

				}
			} else {
				if (ajoutAVL(r.childR, r, false, e)) {
					return equilibreD(r, p, g);
				} else {
					return false;
				}
			}
		}
	}

	private int compareTo(long e, long id2) {
		if (e < id2) {
			return -1;
		}
		if (e > id2) {
			return 1;
		}

		return 0;
	}

	public TesterNode_be getRoot() {
		return root;
	}

}
