package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Tester;
import java.util.Collection;

class TesterRegistration {

    private final Tester tester;
    private final Collection<MethodDescription> methods;

    TesterRegistration(Tester t, Collection<MethodDescription> coll) {
        tester = t;
        methods = coll;
    }

    Tester tester() {
        return tester;
    }

    Collection<MethodDescription> methods() {
        return methods;
    }
}
