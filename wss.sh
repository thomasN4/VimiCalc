#!/bin/sh

## Modifiez la location du dossier lib de votre installation de JavaFX 17 et du fichier .jar s'il vous plaît.
java --module-path "/usr/lib/jvm/javafx-sdk-17.0.7/lib" --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web -jar /opt/VimiCalc-main/out/artifacts/VimiCalc_jar/VimiCalc.jar $1
