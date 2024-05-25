package cloud.user.reactiveusermanagementmicroservice.boundries;

public class AddressBoundary {

    private String country;
    private String city;
    private String zip;

    public AddressBoundary() {

    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }


}
