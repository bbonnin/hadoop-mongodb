package hadoopmongo;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.bson.types.ObjectId;

/**
 * UDF for generating an object id.
 * 
 * @author bbonnin
 *
 */
public class UDFObjectId extends GenericUDF {

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        final List<String> structFieldNames = new ArrayList<String>();
        final List<ObjectInspector> structFieldObjectInspectors = new ArrayList<ObjectInspector>();

        structFieldNames.add("oid");
        structFieldNames.add("bsontype");

        structFieldObjectInspectors.add( PrimitiveObjectInspectorFactory.writableStringObjectInspector );
        structFieldObjectInspectors.add( PrimitiveObjectInspectorFactory.writableIntObjectInspector );

        return ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldObjectInspectors);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        final ObjectId oid = new ObjectId();
        final List<Writable> ret = new ArrayList<Writable>();

        ret.add(new Text(oid.toString()));
        ret.add(new IntWritable(8));

        return ret;
    }

    @Override
    public String getDisplayString(String[] children) {
        return "UDFObjectId";
    }
}
