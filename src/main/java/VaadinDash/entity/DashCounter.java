package VaadinDash.entity;

/**
 * Created by Admin on 05.07.2017.
 */
//@Entity
public class DashCounter {

    @org.springframework.data.annotation.Id
    private String id;

    private long count;

    public DashCounter() {}

    public DashCounter(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Counter[id=%s, count=%d]", id,
                count);
    }



}
