package com.github.ghost93;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.*;

public class Main {
    public static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, ProcessingException {
        JsonSchema geoJsonSchema = getLocalGeoJsonSchema();

        JsonNode homeGeoJson = objectMapper.readTree("{\n\t\"type\": \"Polygon\",\n\t\"coordinates\": [[[35.250506,\n\t31.820620],\n\t[35.250516,\n\t31.820696],\n\t[35.250573,\n\t31.820733],\n\t[35.250599,\n\t31.820692],\n\t[35.250601,\n\t31.820623],\n\t[35.250510,\n\t31.820619],\n\t[35.250506,\n\t31.820620]]]\n}");

        System.out.println("HOME");
        final ProcessingReport validate = geoJsonSchema.validate(homeGeoJson);
        System.out.println(validate.toString());

        final boolean validInstance = geoJsonSchema.validInstance(homeGeoJson);
        System.out.println(validInstance);

        System.out.println("GEOMETRY COLLECTION");

        final JsonNode geometryCollection = objectMapper.readTree("{ \"type\": \"GeometryCollection\",\n    \"geometries\": [\n      { \"type\": \"Point\",\n        \"coordinates\": [100.0, 0.0]\n        },\n      { \"type\": \"LineString\",\n        \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n        }\n    ]\n  }");

        final ProcessingReport collectionValidationReport = geoJsonSchema.validate(geometryCollection);
        System.out.println(collectionValidationReport);

        System.out.println("WIKIPEDIA");

        final ProcessingReport wikipediaValidation = geoJsonSchema.validate(objectMapper.readTree("{ \"type\": \"FeatureCollection\",\n    \"features\": [\n      { \"type\": \"Feature\",\n        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n        \"properties\": {\"prop0\": \"value0\"}\n        },\n      { \"type\": \"Feature\",\n        \"geometry\": {\n          \"type\": \"LineString\",\n          \"coordinates\": [\n            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n            ]\n          },\n        \"properties\": {\n          \"prop0\": \"value0\",\n          \"prop1\": 0.0\n          }\n        },\n      { \"type\": \"Feature\",\n         \"geometry\": {\n           \"type\": \"Polygon\",\n           \"coordinates\": [\n             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n               [100.0, 1.0], [100.0, 0.0] ]\n             ]\n         },\n         \"properties\": {\n           \"prop0\": \"value0\",\n           \"prop1\": {\"this\": \"that\"}\n           }\n         }\n       ]\n     }"));
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

        LoadingConfiguration loadingCfg = LoadingConfiguration.newBuilder().preloadSchema(geoJsonSchemaNode)
                .preloadSchema(geometrySchemaNode)
                .preloadSchema(crsSchemaNode)
                .preloadSchema(bboxSchemaNode)
                .setEnableCache(true).freeze();

        final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingCfg).freeze();
        final JsonSchema res = jsonSchemaFactory.getJsonSchema(geoJsonSchemaNode);
        return res;
    }
}
