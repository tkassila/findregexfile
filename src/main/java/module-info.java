module com.example.findregexfile {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;

  //  opens org.apache.commons.io.filefilter to javafx.fxml;
    opens com.metait.findregexfile to javafx.fxml;
    exports com.metait.findregexfile;
}