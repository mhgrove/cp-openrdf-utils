/*
 * Copyright (c) 2009-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.complexible.common.openrdf.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;
import info.aduna.iteration.Iteration;
import com.google.common.base.Predicate;

/**
 * <p>Utility methods for Aduna {@link info.aduna.iteration.Iteration Iterations} not already present in {@link Iterations}</p>
 *
 * @author  Michael Grove
 * @since   0.4
 * @version 4.0
 */
public final class AdunaIterations {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AdunaIterations.class);

	/**
	 * Private constructor, no instances
	 */
	private AdunaIterations() {
        throw new AssertionError();
	}

	/**
	 * Wrap the given {@link CloseableIteration} as a {@link Stream}
	 *
	 * @param theIter   the iterator to view as a {@code Stream}
	 * @param <T>       the type of results in the Iteration
	 *
	 * @return          the new {@code Stream}
	 */
	public static <T, I extends CloseableIteration<T, ? extends Exception>> Stream<T> stream(final I theIter) {
		final Stream<T> aStream = StreamSupport.stream(new IterationStream<T>(theIter), false);
		aStream.onClose(() -> {
			try {
				theIter.close();
			}
			catch (Exception e) {
				Throwables.propagateIfInstanceOf(e, RuntimeException.class);

				// todo: convert to OpenRdfException when Sesame 4 is available
				throw Throwables.propagate(e);
			}
		});

		return aStream;

//		// sorry about this wrapper.  only way to make sure that the original Iteration is closed since the
//		// wrapper from StreamSupport does the default no-op since Spliterator's normally don't need to be closed
//		// todo: DelegatingStream!
//		return new Stream<T>() {
//			public void close() {
//				try {
//					theIter.close();
//				}
//				catch (Exception e) {
//					Throwables.propagateIfInstanceOf(e, RuntimeException.class);
//
//					// todo: convert to OpenRdfException when Sesame 4 is available
//					throw Throwables.propagate(e);
//				}
//			}
//
//			public IntStream flatMapToInt(final Function<? super T, ? extends IntStream> mapper) {
//				return aStream.flatMapToInt(mapper);
//			}
//
//			public boolean allMatch(final java.util.function.Predicate<? super T> predicate) {
//				return aStream.allMatch(predicate);
//			}
//
//			public boolean anyMatch(final java.util.function.Predicate<? super T> predicate) {
//				return aStream.anyMatch(predicate);
//			}
//
//			public <R, A> R collect(final Collector<? super T, A, R> collector) {
//				return aStream.collect(collector);
//			}
//
//			public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
//				return aStream.collect(supplier, accumulator, combiner);
//			}
//
//			public long count() {
//				return aStream.count();
//			}
//
//			public Stream<T> distinct() {
//				return aStream.distinct();
//			}
//
//			public Stream<T> filter(final java.util.function.Predicate<? super T> predicate) {
//				return aStream.filter(predicate);
//			}
//
//			public java.util.Optional<T> findAny() {
//				return aStream.findAny();
//			}
//
//			public java.util.Optional<T> findFirst() {
//				return aStream.findFirst();
//			}
//
//			public <R> Stream<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> mapper) {
//				return aStream.flatMap(mapper);
//			}
//
//			public DoubleStream flatMapToDouble(final Function<? super T, ? extends DoubleStream> mapper) {
//				return aStream.flatMapToDouble(mapper);
//			}
//
//			public LongStream flatMapToLong(final Function<? super T, ? extends LongStream> mapper) {
//				return aStream.flatMapToLong(mapper);
//			}
//
//			public void forEach(final Consumer<? super T> action) {
//				aStream.forEach(action);
//			}
//
//			public void forEachOrdered(final Consumer<? super T> action) {
//				aStream.forEachOrdered(action);
//			}
//
//			public Stream<T> limit(final long maxSize) {
//				return aStream.limit(maxSize);
//			}
//
//			public <R> Stream<R> map(final Function<? super T, ? extends R> mapper) {
//				return aStream.map(mapper);
//			}
//
//			public DoubleStream mapToDouble(final ToDoubleFunction<? super T> mapper) {
//				return aStream.mapToDouble(mapper);
//			}
//
//			public IntStream mapToInt(final ToIntFunction<? super T> mapper) {
//				return aStream.mapToInt(mapper);
//			}
//
//			public LongStream mapToLong(final ToLongFunction<? super T> mapper) {
//				return aStream.mapToLong(mapper);
//			}
//
//			public java.util.Optional<T> max(final Comparator<? super T> comparator) {
//				return aStream.max(comparator);
//			}
//
//			public java.util.Optional<T> min(final Comparator<? super T> comparator) {
//				return aStream.min(comparator);
//			}
//
//			public boolean noneMatch(final java.util.function.Predicate<? super T> predicate) {
//				return aStream.noneMatch(predicate);
//			}
//
//			public Stream<T> peek(final Consumer<? super T> action) {
//				return aStream.peek(action);
//			}
//
//			public java.util.Optional<T> reduce(final BinaryOperator<T> accumulator) {
//				return aStream.reduce(accumulator);
//			}
//
//			public T reduce(final T identity, final BinaryOperator<T> accumulator) {
//				return aStream.reduce(identity, accumulator);
//			}
//
//			public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
//				return aStream.reduce(identity, accumulator, combiner);
//			}
//
//			public Stream<T> skip(final long n) {
//				return aStream.skip(n);
//			}
//
//			public Stream<T> sorted() {
//				return aStream.sorted();
//			}
//
//			public Stream<T> sorted(final Comparator<? super T> comparator) {
//				return aStream.sorted(comparator);
//			}
//
//			public Object[] toArray() {
//				return aStream.toArray();
//			}
//
//			public <A> A[] toArray(final IntFunction<A[]> generator) {
//				return aStream.toArray(generator);
//			}
//
//			public boolean isParallel() {
//				return aStream.isParallel();
//			}
//
//			public Iterator<T> iterator() {
//				return aStream.iterator();
//			}
//
//			public Stream<T> onClose(final Runnable closeHandler) {
//				return aStream.onClose(closeHandler);
//			}
//
//			public Stream<T> parallel() {
//				return aStream.parallel();
//			}
//
//			public Stream<T> sequential() {
//				return aStream.sequential();
//			}
//
//			public Spliterator<T> spliterator() {
//				return aStream.spliterator();
//			}
//
//			public Stream<T> unordered() {
//				return aStream.unordered();
//			}
//		};
	}

	/**
	 * Conver the Sesame Iteration to a Java Iterator
	 * @param theIteration the iteration
	 * @param <T> the type returned from the iteration
	 * @return the Iteration as an iterator
	 */
	public static <T> Iterator<T> iterator(Iteration<T, ?> theIteration) {
		return new IterationIterator<T>(theIteration);
	}

	/**
	 * Return the RepositoryResult as an {@link Iterable}.
	 * @param theResult the RepositoryResult to wrap
	 * @return the RepositoryResult as an Iterable
	 */
	public static <T, E extends Exception> Iterable<T> iterable(final Iteration<T, E> theResult) {
		return () -> iterator(theResult);
	}

	/**
	 * Quietly close the iteration
	 * @param theCloseableIteration the iteration to close
	 */
	public static void closeQuietly(final CloseableIteration<?,?> theCloseableIteration) {
		try {
			if (theCloseableIteration != null) {
				Iterations.closeCloseable(theCloseableIteration);
			}
		}
		catch (Exception e) {
			LOGGER.warn("Ignoring error while closing iteration.", e);
		}
	}

    /**
     * Return the results of the Iteration as a {@link Set}
     * @param theIter   the iteration
     * @return          the contents of the Iteration in a Set
     * @throws E        if there is an error while iterating
     */
    public static <T, E extends Exception> Set<T> toSet(final CloseableIteration<T, E> theIter) throws E {
        if (theIter == null) {
            return Collections.emptySet();
        }

        try {
            return Sets.newHashSet(iterable(theIter));
        }
        finally {
            theIter.close();
        }
    }

    /**
     * Return the results of the Iteration as a {@link List}
     * @param theIter   the iteration
     * @return          the contents of the Iteration in a List
     * @throws E        if there is an error while iterating
     */
    public static <T, E extends Exception> List<T> toList(final CloseableIteration<T, E> theIter) throws E {
        if (theIter == null) {
            return Collections.emptyList();
        }

        try {
            return Lists.newArrayList(iterable(theIter));
        }
        finally {
            theIter.close();
        }
    }

    /**
     * Return the first result of the iteration.  If the Iteration is empty or null, the Optional will be absent.
     *
     * The Iteration is closed whether or not there is a result.
     *
     * @param theIter   the iteration
     * @return          an Optional containing the first result of the iteration, if present.
     * @throws E
     */
    public static <T, E extends Exception> Optional<T> singleResult(final CloseableIteration<T, E> theIter) throws E {
        if (theIter == null) {
            return Optional.absent();
        }

        try {
            return theIter.hasNext() ? Optional.of(theIter.next()) : Optional.<T>absent();
        }
        finally {
            theIter.close();
        }
    }

    /**
     * Add all of the elements of the Iteration to the specified {@link Collection}.  The Iteration is closed when complete.
     *
     * @param theIter       the Iteration
     * @param theCollection the collection to add the elements to
     * @return              the Collection
     * @throws E            if there is an error while iteration
     */
    public static <T, E extends Exception, C extends Collection<? super T>> C add(final CloseableIteration<T, E> theIter, final C theCollection) throws E {
        theCollection.addAll(toList(theIter));
        return theCollection;
    }

    /**
     * Consume all of the results in the Iteration and then close it when iteration is complete.
     * @param theIter   the Iteration to consume
     * @throws E        if there is an error while consuming the results
     */
    public static <T, E extends Exception> void consume(final CloseableIteration<T, E> theIter) throws E {
        if (theIter == null) {
            return;
        }

        try {
            while (theIter.hasNext()) {
                theIter.next();
            }
        }
        finally {
            theIter.close();
        }
    }

    /**
     * Return the number of elements left in the Iteration.  The iteration is closed when complete.
     * @param theIter   the Iteration whose size should be computed
     * @return          the number of elements left
     * @throws E        if there is an error while iterating
     */
    public static <T, E extends Exception> long size(final CloseableIteration<T, E> theIter) throws E {
        if (theIter == null) {
            return 0;
        }

        try {
            long aCount = 0;
            while (theIter.hasNext()) {
                theIter.next();
                aCount++;
            }

            return aCount;
        }
        finally {
            theIter.close();
        }
    }
}
