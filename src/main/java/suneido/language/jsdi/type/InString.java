package suneido.language.jsdi.type;

import javax.annotation.concurrent.Immutable;

import suneido.language.jsdi.Buffer;
import suneido.language.jsdi.DllInterface;
import suneido.language.jsdi.Marshaller;

/**
 * TODO: docs
 * @author Victor Schappert
 * @since 20130718
 */
@DllInterface
@Immutable
public final class InString extends StringIndirect {

	//
	// CONSTRUCTORS
	//

	private InString() { }

	//
	// SINGLETON INSTANCE
	//

	public static final InString INSTANCE = new InString();

	
	//
	// ANCESTOR CLASS: Type
	//

	@Override
	public String getDisplayName() {
		return "[in] " + IDENTIFIER_STRING;
	}

	@Override
	public void marshallIn(Marshaller marshaller, Object value) {
		if (value instanceof CharSequence) {
			marshaller.putStringPtr(((CharSequence)value).toString(), false);
		} else if (value instanceof Buffer) {
			marshaller.putStringPtr((Buffer)value, false);
		} else {
			putNullStringPtrOrThrow(marshaller, value, false);
		}
	}

	@Override
	public Object marshallOut(Marshaller marshaller, Object oldValue) {
		// Do nothing, since we don't care about any changes to the value.
		return null == oldValue ? Boolean.FALSE : oldValue;
	}
}
