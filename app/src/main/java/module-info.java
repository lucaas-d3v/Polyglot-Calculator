module com.k.calc {
    requires javafx.controls;

    // Jackson (databind -> annotations + core)
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // Ikonli
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;

    exports com.k.calc;
    exports com.k.calc.backend;
}
