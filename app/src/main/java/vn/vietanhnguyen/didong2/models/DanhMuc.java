package vn.vietanhnguyen.didong2.models;

public class DanhMuc {
    private String id;
    private String name;

    public DanhMuc(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public DanhMuc() {

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
