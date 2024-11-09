package vn.vietanhnguyen.didong2.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Food implements Parcelable {
    private String id;
    private String name;
    private double price;
    private String image;
    private String categoryId;
    private String categoryName;

    public Food(String id, String name, double price, String image, String categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    // Parcelable implementation
    protected Food(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        image = in.readString();
        categoryId = in.readString();
        categoryName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeString(image);
        dest.writeString(categoryId);
        dest.writeString(categoryName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };
}