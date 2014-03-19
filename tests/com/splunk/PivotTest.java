/*
 * Copyright 2014 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.splunk;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PivotTest extends SDKTestCase {
    DataModelObject dataModelObject;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        EntityCollection<DataModel> dataModels = service.getDataModels();

        DataModelArgs args = new DataModelArgs();
        args.setRawDescription(streamToString(openResource("data/datamodels/data_model_for_pivot.json")));
        DataModel model = dataModels.create(createTemporaryName(), args);

        this.dataModelObject = model.getObject("test_data");
        Assert.assertNotNull(dataModelObject);
    }

    @After
    public void tearDown() throws Exception {
        for (DataModel d : service.getDataModels().values()) {
            if (d.getName().startsWith("delete-me")) {
                d.remove();
            }
        }

        super.tearDown();
    }

    @Test
    public void testConstructorArgs() {

        PivotSpecification pivotArgs = new PivotSpecification(dataModelObject);

        JsonObject root = pivotArgs.toJson();

        Assert.assertTrue(root.has("dataModel"));
        Assert.assertEquals(new JsonPrimitive(dataModelObject.getDataModel().getName()), root.get("dataModel"));

        Assert.assertTrue(root.has("baseClass"));
        Assert.assertEquals(new JsonPrimitive(dataModelObject.getName()), root.get("baseClass"));
    }

    @Test
    public void testAccelerationWorks() {
        PivotSpecification pivotArgs = new PivotSpecification(dataModelObject);

        Assert.assertEquals(dataModelObject.getDataModel().getName(), pivotArgs.getNamespace());

        String sid = createTemporaryName();
        pivotArgs.setAccelerationJob(sid);
        Assert.assertEquals("sid="+sid, pivotArgs.getNamespace());

        String namespace = createTemporaryName();
        pivotArgs.setAccelerationNamespace(namespace);
        Assert.assertEquals(namespace, pivotArgs.getNamespace());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddFilterOnNonexistantField() {
        PivotSpecification pivotArgs = new PivotSpecification(dataModelObject);
        pivotArgs.addFilter(createTemporaryName(), BooleanComparison.EQUALS, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddBooleanFilterOnWrongType() {
        PivotSpecification pivotSpec = new PivotSpecification(dataModelObject);
        pivotSpec.addFilter("_time", BooleanComparison.EQUALS, true);
    }

    @Test
    public void testAddBooleanFilter() {
        PivotSpecification pivotSpec = new PivotSpecification(dataModelObject);
        pivotSpec.addFilter("has_boris", BooleanComparison.EQUALS, true);

        Assert.assertEquals(1, pivotSpec.getFilters().size());
        for (PivotFilter pf : pivotSpec.getFilters()) {
            Assert.assertTrue(pf instanceof BooleanPivotFilter);
            JsonElement obj = pf.toJson();
            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("has_boris"), o.get("fieldName"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("boolean"), o.get("type"));

            Assert.assertTrue(o.has("comparator"));
            Assert.assertEquals(new JsonPrimitive("="), o.get("comparator"));

            Assert.assertTrue(o.has("compareTo"));
            Assert.assertEquals(new JsonPrimitive(true), o.get("compareTo"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddStringFilterOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter("has_boris", StringComparison.CONTAINS, "abc");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddStringFilteronNonexistantField() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter(createTemporaryName(), StringComparison.CONTAINS, "abc");
    }

    @Test
    public void testAddStringFilter() {
        PivotSpecification pivotSpec = new PivotSpecification(dataModelObject);
        pivotSpec.addFilter("host", StringComparison.CONTAINS, "abc");

        Assert.assertEquals(1, pivotSpec.getFilters().size());
        for (PivotFilter pf : pivotSpec.getFilters()) {
            Assert.assertTrue(pf instanceof StringPivotFilter);
            JsonElement obj = pf.toJson();
            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("host"), o.get("fieldName"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("string"), o.get("type"));

            Assert.assertTrue(o.has("comparator"));
            Assert.assertEquals(new JsonPrimitive("contains"), o.get("comparator"));

            Assert.assertTrue(o.has("compareTo"));
            Assert.assertEquals(new JsonPrimitive("abc"), o.get("compareTo"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddIpv4FilterOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter("has_boris", IPv4Comparison.STARTS_WITH, "192.168");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIpv4FilterOnNonexistantField() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter(createTemporaryName(), IPv4Comparison.STARTS_WITH, "192.168");
    }

    @Test
    public void testAddIpv4Filter() {
        PivotSpecification pivotSpec = new PivotSpecification(dataModelObject);
        pivotSpec.addFilter("hostip", IPv4Comparison.STARTS_WITH, "192.168");

        Assert.assertEquals(1, pivotSpec.getFilters().size());
        for (PivotFilter pf : pivotSpec.getFilters()) {
            Assert.assertTrue(pf instanceof IPv4PivotFilter);
            JsonElement obj = pf.toJson();
            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("hostip"), o.get("fieldName"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("ipv4"), o.get("type"));

            Assert.assertTrue(o.has("comparator"));
            Assert.assertEquals(new JsonPrimitive("startsWith"), o.get("comparator"));

            Assert.assertTrue(o.has("compareTo"));
            Assert.assertEquals(new JsonPrimitive("192.168"), o.get("compareTo"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddNumberFilterOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter("has_boris", NumberComparison.AT_LEAST, 2.3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNumberFilterOnNonexistantField() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter(createTemporaryName(), NumberComparison.AT_LEAST, 2.3);
    }

    @Test
    public void testAddNumberFilter() {
        PivotSpecification pivotSpec = new PivotSpecification(dataModelObject);
        pivotSpec.addFilter("epsilon", NumberComparison.AT_LEAST, 2.3);

        Assert.assertEquals(1, pivotSpec.getFilters().size());
        for (PivotFilter pf : pivotSpec.getFilters()) {
            Assert.assertTrue(pf instanceof NumberPivotFilter);
            JsonElement obj = pf.toJson();
            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("epsilon"), o.get("fieldName"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("number"), o.get("type"));

            Assert.assertTrue(o.has("comparator"));
            Assert.assertEquals(new JsonPrimitive(">="), o.get("comparator"));

            Assert.assertTrue(o.has("compareTo"));
            Assert.assertEquals(new JsonPrimitive((double)2.3), o.get("compareTo"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddLimitFilterOnNonexistentField() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter("has_boris", "host",
                SortDirection.DEFAULT, 50, StatsFunction.COUNT);
    }

    @Test
    public void testAddLimitFilter() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addFilter("epsilon", "hostip", SortDirection.ASCENDING, 500, StatsFunction.AVERAGE);

        Assert.assertEquals(1, pivotSpecification.getFilters().size());
        for (PivotFilter pf : pivotSpecification.getFilters()) {
            Assert.assertTrue(pf instanceof LimitPivotFilter);
            JsonElement obj = pf.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("epsilon"), o.get("fieldName"));

            Assert.assertTrue(o.has("owner"));
            Assert.assertEquals(new JsonPrimitive("test_data"), o.get("owner"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("number"), o.get("type"));

            Assert.assertTrue(o.has("attributeName"));
            Assert.assertEquals(new JsonPrimitive("hostip"), o.get("attributeName"));

            Assert.assertTrue(o.has("attributeOwner"));
            Assert.assertEquals(new JsonPrimitive("test_data"), o.get("attributeOwner"));

            Assert.assertTrue(o.has("limitType"));
            Assert.assertEquals(new JsonPrimitive("lowest"), o.get("limitType"));

            Assert.assertTrue(o.has("limitAmount"));
            Assert.assertEquals(new JsonPrimitive(500), o.get("limitAmount"));

            Assert.assertTrue(o.has("statsFn"));
            Assert.assertEquals(new JsonPrimitive("average"), o.get("statsFn"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddNumericRowSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("has_boris", "My Label");
    }

    @Test
    public void testAddNumericRowSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("epsilon", "My Label");

        Assert.assertEquals(1, pivotSpecification.getRowSplits().size());
        for (PivotRowSplit prs : pivotSpecification.getRowSplits()) {
            Assert.assertTrue(prs instanceof NumberPivotRowSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("epsilon"), o.get("fieldName"));

            Assert.assertTrue(o.has("owner"));
            Assert.assertEquals(new JsonPrimitive("test_data"), o.get("owner"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("number"), o.get("type"));

            Assert.assertTrue(o.has("label"));
            Assert.assertEquals(new JsonPrimitive("My Label"), o.get("label"));

            Assert.assertTrue(o.has("display"));
            Assert.assertEquals(new JsonPrimitive("all"), o.get("display"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddRangeRowSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("has_boris", "My Label", 0, 100, 20, 5);
    }

    @Test
    public void testAddRangeRowSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("epsilon", "My Label", 0, 100, 20, 5);

        Assert.assertEquals(1, pivotSpecification.getRowSplits().size());
        for (PivotRowSplit prs : pivotSpecification.getRowSplits()) {
            Assert.assertTrue(prs instanceof RangePivotRowSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("epsilon"), o.get("fieldName"));

            Assert.assertTrue(o.has("owner"));
            Assert.assertEquals(new JsonPrimitive("test_data"), o.get("owner"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("number"), o.get("type"));

            Assert.assertTrue(o.has("label"));
            Assert.assertEquals(new JsonPrimitive("My Label"), o.get("label"));

            Assert.assertTrue(o.has("display"));
            Assert.assertEquals(new JsonPrimitive("ranges"), o.get("display"));

            JsonObject ranges = new JsonObject();
            ranges.add("start", new JsonPrimitive(0));
            ranges.add("end", new JsonPrimitive(100));
            ranges.add("size", new JsonPrimitive(20));
            ranges.add("maxNumberOf", new JsonPrimitive(5));
            Assert.assertTrue(o.has("ranges"));
            Assert.assertEquals(ranges, o.get("ranges"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddBooleanRowSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("epsilon", "My Label", "true", "false");
    }

    @Test
    public void testAddBooleanRowSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("has_boris", "My Label", "is_true", "is_false");

        Assert.assertEquals(1, pivotSpecification.getRowSplits().size());
        for (PivotRowSplit prs : pivotSpecification.getRowSplits()) {
            Assert.assertTrue(prs instanceof BooleanPivotRowSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            JsonObject expected = new JsonObject();
            expected.add("fieldName", new JsonPrimitive("has_boris"));
            expected.add("label", new JsonPrimitive("My Label"));
            expected.add("owner", new JsonPrimitive("test_data"));
            expected.addProperty("type", "boolean");
            expected.addProperty("trueLabel", "is_true");
            expected.addProperty("falseLabel", "is_false");

            Assert.assertEquals(expected, o);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddTimestampRowSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("epsilon", "My Label", "true", "false");
    }

    @Test
    public void testAddTimestampRowSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("_time", "My Label", TimestampBinning.DAY);

        Assert.assertEquals(1, pivotSpecification.getRowSplits().size());
        for (PivotRowSplit prs : pivotSpecification.getRowSplits()) {
            Assert.assertTrue(prs instanceof TimestampPivotRowSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            JsonObject expected = new JsonObject();
            expected.add("fieldName", new JsonPrimitive("_time"));
            expected.add("label", new JsonPrimitive("My Label"));
            expected.add("owner", new JsonPrimitive("BaseEvent"));
            expected.addProperty("type", "timestamp");
            expected.addProperty("period", "day");

            Assert.assertEquals(expected, o);
        }
    }

    @Test
    public void testAddStringRowSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("host", "My Label");

        Assert.assertEquals(1, pivotSpecification.getRowSplits().size());
        for (PivotRowSplit prs : pivotSpecification.getRowSplits()) {
            Assert.assertTrue(prs instanceof StringPivotRowSplit);
            JsonElement found = prs.toJson();

            JsonObject expected = new JsonObject();
            expected.addProperty("fieldName", "host");
            expected.addProperty("label", "My Label");
            expected.addProperty("owner", "BaseEvent");
            expected.addProperty("type", "string");

            Assert.assertEquals(expected, found);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddNumericColumnSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("has_boris");
    }

    @Test
    public void testAddNumericColumnSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("epsilon");

        Assert.assertEquals(1, pivotSpecification.getColumnSplits().size());
        for (PivotColumnSplit prs : pivotSpecification.getColumnSplits()) {
            Assert.assertTrue(prs instanceof NumericPivotColumnSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("epsilon"), o.get("fieldName"));

            Assert.assertTrue(o.has("owner"));
            Assert.assertEquals(new JsonPrimitive("test_data"), o.get("owner"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("number"), o.get("type"));

            Assert.assertTrue(o.has("display"));
            Assert.assertEquals(new JsonPrimitive("all"), o.get("display"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddRangeColumnSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("has_boris", 0, 100, 20, 5);
    }

    @Test
    public void testAddRangeColumnSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("epsilon", 0, 100, 20, 5);

        Assert.assertEquals(1, pivotSpecification.getColumnSplits().size());
        for (PivotColumnSplit prs : pivotSpecification.getColumnSplits()) {
            Assert.assertTrue(prs instanceof RangePivotColumnSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            Assert.assertTrue(o.has("fieldName"));
            Assert.assertEquals(new JsonPrimitive("epsilon"), o.get("fieldName"));

            Assert.assertTrue(o.has("owner"));
            Assert.assertEquals(new JsonPrimitive("test_data"), o.get("owner"));

            Assert.assertTrue(o.has("type"));
            Assert.assertEquals(new JsonPrimitive("number"), o.get("type"));

            Assert.assertTrue(o.has("display"));
            Assert.assertEquals(new JsonPrimitive("ranges"), o.get("display"));

            JsonObject ranges = new JsonObject();
            ranges.add("start", new JsonPrimitive(0));
            ranges.add("end", new JsonPrimitive(100));
            ranges.add("size", new JsonPrimitive(20));
            ranges.add("maxNumberOf", new JsonPrimitive(5));
            Assert.assertTrue(o.has("ranges"));
            Assert.assertEquals(ranges, o.get("ranges"));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddBooleanColumnSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("epsilon", "true", "false");
    }

    @Test
    public void testAddBooleanColumnSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("has_boris", "is_true", "is_false");

        Assert.assertEquals(1, pivotSpecification.getColumnSplits().size());
        for (PivotColumnSplit prs : pivotSpecification.getColumnSplits()) {
            Assert.assertTrue(prs instanceof BooleanPivotColumnSplit);
            JsonElement obj = prs.toJson();

            Assert.assertTrue(obj instanceof JsonObject);
            JsonObject o = (JsonObject)obj;

            JsonObject expected = new JsonObject();
            expected.add("fieldName", new JsonPrimitive("has_boris"));
            expected.add("owner", new JsonPrimitive("test_data"));
            expected.addProperty("type", "boolean");
            expected.addProperty("trueLabel", "is_true");
            expected.addProperty("falseLabel", "is_false");

            Assert.assertEquals(expected, o);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddTimestampColumnSplitOnWrongType() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("epsilon", "true", "false");
    }

    @Test
    public void testAddTimestampColumnSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("_time", TimestampBinning.DAY);

        Assert.assertEquals(1, pivotSpecification.getColumnSplits().size());
        for (PivotColumnSplit prs : pivotSpecification.getColumnSplits()) {
            Assert.assertTrue(prs instanceof TimestampPivotColumnSplit);
            JsonObject found = prs.toJson();

            JsonObject expected = new JsonObject();
            expected.add("fieldName", new JsonPrimitive("_time"));
            expected.add("owner", new JsonPrimitive("BaseEvent"));
            expected.addProperty("type", "timestamp");
            expected.addProperty("period", "day");

            Assert.assertEquals(expected, found);
        }
    }

    @Test
    public void testAddStringColumnSplit() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addColumnSplit("host");

        Assert.assertEquals(1, pivotSpecification.getColumnSplits().size());
        for (PivotColumnSplit prs : pivotSpecification.getColumnSplits()) {
            Assert.assertTrue(prs instanceof StringPivotColumnSplit);
            JsonObject found = prs.toJson();

            JsonObject expected = new JsonObject();
            expected.addProperty("fieldName", "host");
            expected.addProperty("owner", "BaseEvent");
            expected.addProperty("type", "string");

            Assert.assertEquals(expected, found);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNonexistantFieldToCellValue() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addCellValue("nonexistant", "my_label", StatsFunction.COUNT, false);
    }

    @Test
    public void testAddStringCellValue() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addCellValue("source", "Source Value", StatsFunction.DISTINCT_COUNT, true);

        Assert.assertEquals(1, pivotSpecification.getCellValues().size());
        for (PivotCellValue pcv : pivotSpecification.getCellValues()) {
            JsonObject found = pcv.toJson();
            JsonObject expected = new JsonObject();
            expected.addProperty("fieldName", "source");
            expected.addProperty("owner", "BaseEvent");
            expected.addProperty("type", "string");
            expected.addProperty("label", "Source Value");
            expected.addProperty("value", "dc");
            expected.addProperty("sparkline", true);

            Assert.assertEquals(expected, found);
        }
    }

    @Test
    public void testAddIpv4CellValue() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addCellValue("hostip", "Source Value", StatsFunction.DISTINCT_COUNT, true);

        Assert.assertEquals(1, pivotSpecification.getCellValues().size());
        for (PivotCellValue pcv : pivotSpecification.getCellValues()) {
            JsonObject found = pcv.toJson();
            JsonObject expected = new JsonObject();
            expected.addProperty("fieldName", "hostip");
            expected.addProperty("owner", "test_data");
            expected.addProperty("type", "ipv4");
            expected.addProperty("label", "Source Value");
            expected.addProperty("value", "dc");
            expected.addProperty("sparkline", true);

            Assert.assertEquals(expected, found);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIllegalStatsFunction() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addCellValue("source", "Source Value", StatsFunction.SUM, true);

    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoBooleanCellValues() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addCellValue("has_boris", "Source Value", StatsFunction.DISTINCT_VALUES, true);
    }

    @Test(expected= HttpException.class)
    public void testEmptyPivotGivesError() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        Pivot pivot = pivotSpecification.pivot();
    }

    @Test
    public void testSimplePivotWithoutNamespace() {
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("has_boris", "Has Boris", "meep", "hilda");

        Pivot pivot = pivotSpecification.pivot();
        Assert.assertNull(pivot.getAcceleratedQuery());
        Assert.assertTrue(pivot.getPivotQuery().startsWith("| pivot"));
    }

    @Test
    public void testSimplePivotWithNamespace() {
        Job adhocJob = dataModelObject.createLocalAccelerationJob();
        PivotSpecification pivotSpecification = new PivotSpecification(dataModelObject);
        pivotSpecification.addRowSplit("has_boris", "Has Boris", "meep", "hilda");

        Pivot pivot = pivotSpecification.pivot(adhocJob);
        Assert.assertNotNull(pivot.getAcceleratedQuery());

        final Job job = pivot.run();
        assertEventuallyTrue(new EventuallyTrueBehavior() {
            @Override
            public boolean predicate() {
                return job.isReady();
            }
        });

        Assert.assertTrue(job.getSearch().startsWith("| tstats"));
    }
}
