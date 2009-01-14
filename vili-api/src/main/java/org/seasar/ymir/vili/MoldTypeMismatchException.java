package org.seasar.ymir.vili;

public class MoldTypeMismatchException extends Exception {
    private static final long serialVersionUID = 1L;

    private Mold mold;

    public MoldTypeMismatchException(Mold mold) {
        this.mold = mold;
    }

    public Mold getMold() {
        return mold;
    }
}
