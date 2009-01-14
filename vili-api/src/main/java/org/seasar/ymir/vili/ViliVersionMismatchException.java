package org.seasar.ymir.vili;

public class ViliVersionMismatchException extends Exception {
    private static final long serialVersionUID = 1L;

    private Mold mold;

    public ViliVersionMismatchException(Mold mold) {
        this.mold = mold;
    }

    public Mold getMold() {
        return mold;
    }
}
