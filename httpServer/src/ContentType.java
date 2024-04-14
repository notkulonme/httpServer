import java.util.ArrayList;


//this class is for identifying data types and other stuff
//the code explains its self I guess
public class ContentType {
    private final ArrayList<String> image;
    private final ArrayList<String> text;

    public ContentType() {
        image = new ArrayList<>();
        image.add("jpg");
        image.add("jpeg");
        image.add("png");
        image.add("svg");
        image.add("webp");
        image.add("ico");
        image.add("jfif");
        image.add("pjpeg");
        image.add("pjp");
        image.add("avif");
        image.add("bmp");
        image.add("tiff");
        image.add("tif");
        image.add("giff");

        text = new ArrayList<>();
        text.add("css");
        text.add("csv");
        text.add("html");
        text.add("js");
        text.add("txt");
        text.add("xml");
        text.add("pdf");
        text.add("qz");
        text.add("json");

    }
    public Boolean isImage(String type) {
        return this.image.contains(type);
    }
    public Boolean isText(String type){
        return this.text.contains(type);
    }
}
