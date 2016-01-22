package com.github.ghost93;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, ProcessingException {
        JsonSchema geoJsonSchema = getLocalGeoJsonSchema();

        JsonNode homeGeoJson = objectMapper.readTree("{\"type\": \"Polygon\",\"coordinates\": [[[35.250506,31.820620],[35.250516,31.820696],[35.250573,31.820733],[35.250599,31.820692],[35.250601,31.820623],[35.250510,31.820619],[35.250506,31.820620]]]}");

        System.out.println("HOME");
        final ProcessingReport validate = geoJsonSchema.validate(homeGeoJson);
        System.out.println(validate.toString());

        final boolean validInstance = geoJsonSchema.validInstance(homeGeoJson);
        System.out.println(validInstance);

        System.out.println("GEOMETRY COLLECTION");

        final JsonNode geometryCollection = objectMapper.readTree("{ \"type\": \"GeometryCollection\",\"geometries\": [{ \"type\": \"Point\",  \"coordinates\": [100.0, 0.0]  },{ \"type\": \"LineString\",  \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]  }]  }");

        final ProcessingReport collectionValidationReport = geoJsonSchema.validate(geometryCollection);
        System.out.println(collectionValidationReport);

        System.out.println("WIKIPEDIA");

        final ProcessingReport wikipediaValidation = geoJsonSchema.validate(objectMapper.readTree("{ \"type\": \"FeatureCollection\",\"features\": [{ \"type\": \"Feature\",  \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},  \"properties\": {\"prop0\": \"value0\"}  },{ \"type\": \"Feature\",  \"geometry\": {\"type\": \"LineString\",\"coordinates\": [[102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]]},  \"properties\": {\"prop0\": \"value0\",\"prop1\": 0.0}  },{ \"type\": \"Feature\",\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],[100.0, 1.0], [100.0, 0.0] ] ]},\"properties\": {\"prop0\": \"value0\",\"prop1\": {\"this\": \"that\"}}} ]}"));
        System.out.println(wikipediaValidation);
    }

    private static JsonSchema getLocalGeoJsonSchema() throws IOException, ProcessingException {
        final String baseSchemaPath = "schemas/sample-json-schemas-master/geojson/";

        InputStream geoJsonSchema = new FileInputStream(new File(String.format("%sgeojson.json", baseSchemaPath)));
        InputStream geometrySchema = new FileInputStream(new File(String.format("%sgeometry.json", baseSchemaPath)));
        InputStream crsSchema = new FileInputStream(new File(String.format("%scrs.json", baseSchemaPath)));
        InputStream bboxSchema = new FileInputStream(new File(String.format("%sbbox.json", baseSchemaPath)));

        final JsonNode geoJsonSchemaNode = objectMapper.readTree(geoJsonSchema);
        final JsonNode geometrySchemaNode = objectMapper.readTree(geometrySchema);
        final JsonNode crsSchemaNode = objectMapper.readTree(crsSchema);
        final JsonNode bboxSchemaNode = objectMapper.readTree(bboxSchema);

        LoadingConfiguration loadingCfg = LoadingConfiguration.newBuilder()
                .preloadSchema(geoJsonSchemaNode)
                .preloadSchema(geometrySchemaNode)
                .preloadSchema(crsSchemaNode)
                .preloadSchema(bboxSchemaNode)
                .setEnableCache(true)
                .freeze();

        final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder()
                .setLoadingConfiguration(loadingCfg)
                .freeze();
        final JsonSchema res = jsonSchemaFactory.getJsonSchema(geoJsonSchemaNode);
        return res;
    }
}
