package base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MutantTesterTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest(name = "{0}")
    @MethodSource("argumentProvider")
    void isMutant(String testName, DNASampleTestCase testCase) {
        Assertions.assertEquals(testCase.isExpected(), MutantTester.isMutant(testCase.getDna()));
    }

    private static Stream<Arguments> argumentProvider() throws IOException {

        List<File> files = getResourceFolderFiles("dna-samples");

        return files.stream()
                .map(file -> Pair.of(file.getName(), parseAsDNASampleTestCase(file)))
                .filter(pair -> pair.getRight() != null)
                .map(pair -> Arguments.arguments(pair.getLeft(), pair.getRight()));
    }

    private static List<File> getResourceFolderFiles(String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        return Arrays.asList(new File(path).listFiles());
    }

    private static DNASampleTestCase parseAsDNASampleTestCase(final File file){
        try {
            return mapper.readValue(file, DNASampleTestCase.class);
        } catch (IOException e) {
            return null;
        }
    };

}
