import sbtavro.SbtAvro._

avroSettings

sourceDirectory in avroConfig := new File("src/main/resources/es/imediava/")

javaSource in avroConfig <<= (sourceDirectory in Compile)(_ / "java")

//stringType in avroConfig := "String"

version in avroConfig := "1.7.5"
