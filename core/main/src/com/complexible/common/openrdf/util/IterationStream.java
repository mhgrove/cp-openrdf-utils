package com.complexible.common.openrdf.util;

	import java.util.Spliterator;
	import java.util.Spliterators;
	import java.util.function.Consumer;

	import com.google.common.base.Throwables;
	import info.aduna.iteration.CloseableIteration;

/**
 * <p>Class which wraps a {@link CloseableIteration} as a {@link Spliterator}.  Resulting iterator is
 * defined to be {@link Spliterator#IMMUTABLE}, {@link Spliterator#NONNULL}, and {@link Spliterator#SORTED}.</p>
 *
 * <p>Note that this does <b>NOT</b> close the underlying {@code CloseableIteration}.  That should be done externally
 * or </p>
 *
 * @author  Michael Grove
 * @since   4.0
 * @version 4.0
 */
public final class IterationStream<T> extends Spliterators.AbstractSpliterator<T> {
	private final CloseableIteration<T, ? extends Exception> mIteration;

	public <I extends CloseableIteration<T, ? extends Exception>> IterationStream(final I theIter) {
		super(Long.MAX_VALUE, Spliterator.IMMUTABLE | Spliterator.NONNULL);

		mIteration = theIter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean tryAdvance(final Consumer<? super T> theAction) {
		boolean aHasNext = false;
		try {
			aHasNext = mIteration.hasNext();

			if (aHasNext) {
				theAction.accept(mIteration.next());
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			Throwables.propagateIfInstanceOf(e, RuntimeException.class);

			// todo: throw OpenRdfException once 4.0 is available as its a runtime exception
			throw Throwables.propagate(e);
		}
		finally {
			if (!aHasNext) {
				AdunaIterations.closeQuietly(mIteration);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forEachRemaining(final Consumer<? super T> theAction) {
		try {
			while (mIteration.hasNext()) {
				theAction.accept(mIteration.next());
			}
		}
		catch (Exception e) {
			Throwables.propagateIfInstanceOf(e, RuntimeException.class);

			// todo: throw OpenRdfException once 4.0 is available as its a runtime exception
			throw Throwables.propagate(e);
		}
		finally {
			try {
				mIteration.close();
			}
			catch (Exception e) {
				Throwables.propagateIfInstanceOf(e, RuntimeException.class);

				// todo: throw OpenRdfException once 4.0 is available as its a runtime exception
				throw Throwables.propagate(e);
			}
		}
	}
}
