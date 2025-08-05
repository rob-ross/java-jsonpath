package org.killeroonie.jsonpath;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Dummy implementations for imported classes


class Query implements Iterable<JSONPathMatch> {
    private final Iterable<JSONPathMatch> matches;
    private final JSONPathEnvironment env;

    public Query(Iterable<JSONPathMatch> matches, JSONPathEnvironment env) {
        this.matches = matches;
        this.env = env;
    }

    @Override
    public Iterator<JSONPathMatch> iterator() {
        return matches.iterator();
    }
}


interface JSONPathSelector {
    Iterable<JSONPathMatch> resolve(Iterable<JSONPathMatch> matches);
    CompletableFuture<Stream<JSONPathMatch>> resolveAsync(CompletableFuture<Stream<JSONPathMatch>> matches);
}

class IndexSelector implements JSONPathSelector {
    @Override
    public Iterable<JSONPathMatch> resolve(Iterable<JSONPathMatch> matches) {
        // Dummy implementation
        return matches;
    }

    @Override
    public CompletableFuture<Stream<JSONPathMatch>> resolveAsync(CompletableFuture<Stream<JSONPathMatch>> matches) {
        // Dummy implementation
        return matches;
    }
}

class ListSelector implements JSONPathSelector {
    public final List<JSONPathSelector> items;

    public ListSelector(List<JSONPathSelector> items) {
        this.items = items;
    }

    @Override
    public Iterable<JSONPathMatch> resolve(Iterable<JSONPathMatch> matches) {
        // Dummy implementation
        return matches;
    }

    @Override
    public CompletableFuture<Stream<JSONPathMatch>> resolveAsync(CompletableFuture<Stream<JSONPathMatch>> matches) {
        // Dummy implementation
        return matches;
    }
}

class PropertySelector implements JSONPathSelector {
    @Override
    public Iterable<JSONPathMatch> resolve(Iterable<JSONPathMatch> matches) {
        // Dummy implementation
        return matches;
    }

    @Override
    public CompletableFuture<Stream<JSONPathMatch>> resolveAsync(CompletableFuture<Stream<JSONPathMatch>> matches) {
        // Dummy implementation
        return matches;
    }
}


/**
 * Multiple JSONPaths combined.
 */
class CompoundJSONPath {
    public final JSONPathEnvironment env;
    public final Object path; // Can be JSONPath or CompoundJSONPath
    public final List<PathOperation> paths;

    public static class PathOperation {
        public final String op;
        public final JSONPath path;

        public PathOperation(String op, JSONPath path) {
            this.op = op;
            this.path = path;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof PathOperation)) return false;
            PathOperation otherOp = (PathOperation) other;
            return op.equals(otherOp.op) && path.equals(otherOp.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(op, path);
        }
    }

    public CompoundJSONPath(JSONPathEnvironment env, Object path, Iterable<PathOperation> paths) {
        this.env = env;
        this.path = path;
        this.paths = new ArrayList<>();
        for (PathOperation pathOp : paths) {
            this.paths.add(pathOp);
        }
    }

    public CompoundJSONPath(JSONPathEnvironment env, Object path) {
        this(env, path, new ArrayList<>());
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(path.toString());
        for (PathOperation pathOp : paths) {
            buf.append(" ").append(pathOp.op).append(" ");
            buf.append(pathOp.path.toString());
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CompoundJSONPath)) {
            return false;
        }
        CompoundJSONPath otherPath = (CompoundJSONPath) other;
        return path.equals(otherPath.path) && paths.equals(otherPath.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, paths);
    }

    /**
     * Find all objects in data matching the given JSONPath path.
     *
     * If data is a string or a file-like object, it will be loaded
     * using JSON parsing and the default JSONDecoder.
     *
     * @param data A JSON document or Java object implementing the List or Map interfaces.
     * @param filterContext Arbitrary data made available to filters using the filter context selector.
     * @return A list of matched objects. If there are no matches, the list will be empty.
     * @throws RuntimeException If the path is invalid or if a filter expression attempts to use types
     *                         in an incompatible way.
     */
    public List<Object> findall(Object data, FilterContextVars filterContext) {
        List<Object> objs;
        if (path instanceof JSONPath) {
            objs = new ArrayList<>(((JSONPath) path).findall(data, filterContext));
        } else {
            objs = new ArrayList<>(((CompoundJSONPath) path).findall(data, filterContext));
        }

        for (PathOperation pathOp : paths) {
            List<Object> _objs = pathOp.path.findall(data, filterContext);
            if (pathOp.op.equals(env.unionToken)) {
                objs.addAll(_objs);
            } else {
                assert pathOp.op.equals(env.intersectionToken) : pathOp.op;
                objs = objs.stream().filter(_objs::contains).collect(Collectors.toList());
            }
        }

        return objs;
    }

    public List<Object> findall(Object data) {
        return findall(data, null);
    }

    /**
     * Generate JSONPathMatch objects for each match.
     *
     * If data is a string or a file-like object, it will be loaded
     * using JSON parsing and the default JSONDecoder.
     *
     * @param data A JSON document or Java object implementing the List or Map interfaces.
     * @param filterContext Arbitrary data made available to filters using the filter context selector.
     * @return An iterator yielding JSONPathMatch objects for each match.
     * @throws RuntimeException If the path is invalid or if a filter expression attempts to use types incompatibly.
     */
    public Iterable<JSONPathMatch> finditer(Object data, FilterContextVars filterContext) {
        Iterable<JSONPathMatch> matches;
        if (path instanceof JSONPath) {
            matches = ((JSONPath) path).finditer(data, filterContext);
        } else {
            matches = ((CompoundJSONPath) path).finditer(data, filterContext);
        }

        for (PathOperation pathOp : paths) {
            Iterable<JSONPathMatch> _matches = pathOp.path.finditer(data, filterContext);
            if (pathOp.op.equals(env.unionToken)) {
                Iterable<JSONPathMatch> finalMatches1 = matches;
                matches = () -> Stream.concat(
                        StreamSupport.stream(finalMatches1.spliterator(), false),
                        StreamSupport.stream(_matches.spliterator(), false)
                ).iterator();
            } else {
                assert pathOp.op.equals(env.intersectionToken);
                List<Object> _objs = StreamSupport.stream(_matches.spliterator(), false)
                        .map(match -> match.obj)
                        .toList();
                Iterable<JSONPathMatch> finalMatches = matches;
                matches = () -> StreamSupport.stream(finalMatches.spliterator(), false)
                        .filter(match -> _objs.contains(match.obj))
                        .iterator();
            }
        }

        return matches;
    }

    public Iterable<JSONPathMatch> finditer(Object data) {
        return finditer(data, null);
    }

    /**
     * Return a JSONPathMatch instance for the first object found in data.
     *
     * null is returned if there are no matches.
     *
     * @param data A JSON document or Java object implementing the List or Map interfaces.
     * @param filterContext Arbitrary data made available to filters using the filter context selector.
     * @return A JSONPathMatch object for the first match, or null if there were no matches.
     * @throws RuntimeException If the path is invalid or if a filter expression attempts to use types
     *                         in an incompatible way.
     */
    public JSONPathMatch match(Object data, FilterContextVars filterContext) {
        Iterator<JSONPathMatch> iterator = finditer(data, filterContext).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public JSONPathMatch match(Object data) {
        return match(data, null);
    }

    /**
     * An async version of findall().
     */
    public CompletableFuture<List<Object>> findallAsync(Object data, FilterContextVars filterContext) {
        CompletableFuture<List<Object>> objsFuture;
        if (path instanceof JSONPath) {
            objsFuture = ((JSONPath) path).findallAsync(data, filterContext);
        } else {
            objsFuture = ((CompoundJSONPath) path).findallAsync(data, filterContext);
        }

        return objsFuture.thenCompose(objs -> {
            List<CompletableFuture<List<Object>>> futures = new ArrayList<>();
            for (PathOperation pathOp : paths) {
                futures.add(pathOp.path.findallAsync(data, filterContext));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<Object> result = new ArrayList<>(objs);
                        for (int i = 0; i < paths.size(); i++) {
                            PathOperation pathOp = paths.get(i);
                            List<Object> _objs = futures.get(i).join();
                            if (pathOp.op.equals(env.unionToken)) {
                                result.addAll(_objs);
                            } else {
                                assert pathOp.op.equals(env.intersectionToken);
                                result = result.stream().filter(_objs::contains).collect(Collectors.toList());
                            }
                        }
                        return result;
                    });
        });
    }

    public CompletableFuture<List<Object>> findallAsync(Object data) {
        return findallAsync(data, null);
    }

    /**
     * An async version of finditer().
     */
    public CompletableFuture<Stream<JSONPathMatch>> finditerAsync(Object data, FilterContextVars filterContext) {
        CompletableFuture<Stream<JSONPathMatch>> matchesFuture;
        if (path instanceof JSONPath) {
            matchesFuture = ((JSONPath) path).finditerAsync(data, filterContext);
        } else {
            matchesFuture = ((CompoundJSONPath) path).finditerAsync(data, filterContext);
        }

        return matchesFuture.thenCompose(matches -> {
            CompletableFuture<Stream<JSONPathMatch>> result = CompletableFuture.completedFuture(matches);

            for (PathOperation pathOp : paths) {
                result = result.thenCompose(currentMatches -> {
                    return pathOp.path.finditerAsync(data, filterContext)
                            .thenApply(_matches -> {
                                if (pathOp.op.equals(env.unionToken)) {
                                    return Stream.concat(currentMatches, _matches);
                                } else {
                                    assert pathOp.op.equals(env.intersectionToken);
                                    List<Object> _objs = _matches.map(match -> match.obj).collect(Collectors.toList());
                                    return currentMatches.filter(match -> _objs.contains(match.obj));
                                }
                            });
                });
            }

            return result;
        });
    }

    public CompletableFuture<Stream<JSONPathMatch>> finditerAsync(Object data) {
        return finditerAsync(data, null);
    }

    /**
     * Return a Query iterator over matches found by applying this path to data.
     *
     * @param data A JSON document or Java object implementing the List or Map interfaces.
     * @param filterContext Arbitrary data made available to filters using the filter context selector.
     * @return A query iterator.
     * @throws RuntimeException If the path is invalid or if a filter expression attempts to use types
     *                         in an incompatible way.
     */
    public Query query(Object data, FilterContextVars filterContext) {
        return new Query(finditer(data, filterContext), env);
    }

    public Query query(Object data) {
        return query(data, null);
    }

    /**
     * Union of this path and another path.
     */
    public CompoundJSONPath union(JSONPath path) {
        List<PathOperation> newPaths = new ArrayList<>(paths);
        newPaths.add(new PathOperation(env.unionToken, path));
        return new CompoundJSONPath(env, this.path, newPaths);
    }

    /**
     * Intersection of this path and another path.
     */
    public CompoundJSONPath intersection(JSONPath path) {
        List<PathOperation> newPaths = new ArrayList<>(paths);
        newPaths.add(new PathOperation(env.intersectionToken, path));
        return new CompoundJSONPath(env, this.path, newPaths);
    }
}

/**
 * Utility function equivalent to Python's _achain for async iteration.
 * This is a helper method that would typically be used internally.
 */
class AsyncChainUtil {
    public static <T> CompletableFuture<Stream<T>> achain(CompletableFuture<Stream<T>>... iterables) {
        return CompletableFuture.allOf(iterables)
                .thenApply(v -> Arrays.stream(iterables)
                        .map(CompletableFuture::join)
                        .reduce(Stream.empty(), Stream::concat));
    }
}
