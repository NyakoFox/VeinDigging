package gay.nyako.veindigging;

public interface PlayerEntityAccess {
    boolean isVeinDigging();
    void setVeinDigging(boolean isExcavating);

    boolean veindigging$usingClientMod();
    void veindigging$setUsingClientMod(boolean usingClientMod);
}
