package test;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.util.TupleBuilder;

public class TestBuildAllTuples {
    ArrayList<BaseModelElement> elements = new ArrayList<BaseModelElement>();

    @Before
    public void setUp() throws Exception {
        BaseModelElement element0 = new BaseModelElement(3, "test");
        BaseModelElement element1 = new BaseModelElement(2, "test 1");
        BaseModelElement element2 = new BaseModelElement(3, "test 2");
        elements.add(element0);
        elements.add(element1);
        elements.add(element2);
    }

    @Ignore
    @Test
    public void testBuildAllTuples() {
        BaseModel model = new BaseModel();
        for (BaseModelElement element : elements) {
            model.addElement(element);
        }
        model.setOrder(2);
        model.setTwise(2);
        TupleBuilder.buildAllTuples(model);
    }
}
