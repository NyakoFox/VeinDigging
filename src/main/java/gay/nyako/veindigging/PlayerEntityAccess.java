package gay.nyako.veindigging;

public interface PlayerEntityAccess {
    boolean isVeinDigging();
    void setVeinDigging(boolean isExcavating);

    boolean usingClientMod();
    void setUsingClientMod(boolean usingClientMod);
}
