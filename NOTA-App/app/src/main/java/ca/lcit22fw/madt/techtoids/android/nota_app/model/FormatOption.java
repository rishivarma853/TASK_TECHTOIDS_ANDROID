package ca.lcit22fw.madt.techtoids.android.nota_app.model;

public class FormatOption {
    public final OnOptionClickListener clickListener;
    private int icon;

    public FormatOption(int icon, OnOptionClickListener clickListener) {
        this.icon = icon;
        this.clickListener = clickListener;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public interface OnOptionClickListener {
        void onItemClick();
    }
}
