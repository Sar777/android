package instinctools.android.readers.json.transformers;

/**
 * Created by orion on 2.1.17.
 */

public interface ITransformer<T> {
    T transform(Object object);
}