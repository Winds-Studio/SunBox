package cn.dreeam.sunbox.benchmarks.jmh;

import cn.dreeam.sunbox.util.Base64Coder;
import org.openjdk.jmh.annotations.*;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

/*
Estimate running time: ~3mins
Target patch: NONE
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class Base64Codec {

    // Copied from org.bukkit.util.io.BukkitObjectStreamTest
    private static final String data = "rO0ABXNyADZjb20uZ29vZ2xlLmNvbW1vbi5jb2xsZWN0LkltbXV0YWJsZUxpc3QkU2VyaWFsaXplZEZvcm0AAAAAAAAAAAIAAVsACGVsZW1lbnRzdAATW0xqYXZhL2xhbmcvT2JqZWN0O3hwdXIAE1tMamF2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAAA3NyABpvcmcuYnVra2l0LnV0aWwuaW8uV3JhcHBlcvJQR+zxEm8FAgABTAADbWFwdAAPTGphdmEvdXRpbC9NYXA7eHBzcgA1Y29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFibGVNYXAkU2VyaWFsaXplZEZvcm0AAAAAAAAAAAIAAlsABGtleXNxAH4AAVsABnZhbHVlc3EAfgABeHB1cQB+AAMAAAAGdAACPT10AAdmbGlja2VydAAFdHJhaWx0AAZjb2xvcnN0AAtmYWRlLWNvbG9yc3QABHR5cGV1cQB+AAMAAAAGdAAIRmlyZXdvcmtzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHABc3EAfgATAHNxAH4AAHVxAH4AAwAAAAJzcQB+AAVzcQB+AAh1cQB+AAMAAAAEcQB+AAt0AANSRUR0AARCTFVFdAAFR1JFRU51cQB+AAMAAAAEdAAFQ29sb3JzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAAHEAfgAicQB+ACJzcQB+AAVzcQB+AAh1cQB+AAMAAAAEcQB+AAtxAH4AG3EAfgAccQB+AB11cQB+AAMAAAAEcQB+AB9zcQB+ACAAAADAc3EAfgAgAAAAwHNxAH4AIAAAAMBzcQB+AAB1cQB+AAMAAAABc3EAfgAFc3EAfgAIdXEAfgADAAAABHEAfgALcQB+ABtxAH4AHHEAfgAddXEAfgADAAAABHEAfgAfc3EAfgAgAAAA/3NxAH4AIAAAAP9zcQB+ACAAAAD/dAAKQkFMTF9MQVJHRXNxAH4ABXNxAH4ACHVxAH4AAwAAAAZxAH4AC3EAfgAMcQB+AA1xAH4ADnEAfgAPcQB+ABB1cQB+AAMAAAAGcQB+ABJxAH4AFXEAfgAVc3EAfgAAdXEAfgADAAAAAXNxAH4ABXNxAH4ACHVxAH4AAwAAAARxAH4AC3EAfgAbcQB+ABxxAH4AHXVxAH4AAwAAAARxAH4AH3EAfgAic3EAfgAgAAAAgHEAfgAic3EAfgAAdXEAfgADAAAAAHQABEJBTExzcQB+AAVzcQB+AAh1cQB+AAMAAAAGcQB+AAtxAH4ADHEAfgANcQB+AA5xAH4AD3EAfgAQdXEAfgADAAAABnEAfgAScQB+ABRxAH4AFHNxAH4AAHVxAH4AAwAAAAFzcQB+AAVzcQB+AAh1cQB+AAMAAAAEcQB+AAtxAH4AG3EAfgAccQB+AB11cQB+AAMAAAAEcQB+AB9zcQB+ACAAAACAcQB+ACJxAH4AInEAfgA/dAAHQ1JFRVBFUg==";

    @Benchmark
    public String javaBase64Mime() {
        final byte[] decoded = Base64.getMimeDecoder().decode(data);

        return Base64.getEncoder().encodeToString(decoded);
    }

    @Benchmark
    public String javaBase64NoMime() {
        final byte[] decoded = Base64.getDecoder().decode(data);

        return Base64.getEncoder().encodeToString(decoded);
    }

    @Benchmark
    public char[] snakeyamlBase64Coder() {
        final byte[] decoded = Base64Coder.decode(data);

        return Base64Coder.encode(decoded);
    }
}
