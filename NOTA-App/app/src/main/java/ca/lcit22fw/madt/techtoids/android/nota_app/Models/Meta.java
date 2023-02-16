package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

public class Meta {
    private String _id;
    private String _title;
    private String _createdDate;
    private String _modifiedDate;

    public Meta(String id, String title) {
        this._id = id;
        this._title = title;
        setLiveCreatedDate();
        setLiveModifiedDate();
//        this._createdDate = createdDate;
//        this._modifiedDate = createdDate;
    }
    public String getId() {
        return this._id;
    }
    public void setId(String id) {
        this._id = id;
    }
    public String getTitle() {
        return this._title;
    }
    public void setTitle(String title) {
        this._title = title;
    }
    public String getCreatedDate() {
        return this._createdDate;
    }
    public void setCreatedDate(String createdDate) {
        this._createdDate = createdDate;
    }
    public void setLiveCreatedDate() {

    }

    public String getModifiedDate() {
        return this._modifiedDate;
    }
    public void setModifiedDate(String modifiedDate) {
        this._modifiedDate = modifiedDate;
    }
    public void setLiveModifiedDate() {

    }
}
