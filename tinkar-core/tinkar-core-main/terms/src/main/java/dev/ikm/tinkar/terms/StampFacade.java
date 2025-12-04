package dev.ikm.tinkar.terms;

public interface StampFacade
        extends dev.ikm.tinkar.component.Stamp, EntityFacade {

    static StampFacade make(int nid) {
        return EntityProxy.Stamp.make(nid);
    }

    static int toNid(StampFacade facade) {
        return facade.nid();
    }

}
