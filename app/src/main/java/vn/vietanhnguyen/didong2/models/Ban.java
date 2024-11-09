package vn.vietanhnguyen.didong2.models;


public class Ban {
    private String id;
    private String name;
    private String description;
    private String status;

    public Ban() {
    }

    public Ban(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}