package edu360.bike2bike.bike.DataSource;

public class BikeInfornmation {
    private String id;
    private int status;
    private Double latitude;
    private Double longitude;
    private String qrCode;

    @Override
    public String toString() {
        return "BikeInfornmation{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", qrcode='" + qrCode + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getQrcode() {
        return qrCode;
    }

    public void setQrcode(String qrcode) {
        this.qrCode = qrcode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
