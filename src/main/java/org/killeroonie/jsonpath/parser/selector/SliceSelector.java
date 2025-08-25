package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

import java.util.Optional;


public class SliceSelector extends JSONPathSelector {

    private final Optional<Long> start;
    private final Optional<Long> stop;
    private final Optional<Long> step;

    public SliceSelector(JSONPathEnvironment env, Token token, Long start, Long stop, Long step) {
        super(env, token);
        this.start = Optional.ofNullable(start);
        this.stop  = Optional.ofNullable(stop);
        this.step  = Optional.ofNullable(step);
        validate();
    }

    private void validate() {
        start.ifPresent(startInt -> checkRange(startInt, "start"));
        stop.ifPresent(aLong -> checkRange(aLong, "stop"));
        step.ifPresent(aLong -> checkRange(aLong, "step"));
    }

    @Override
    public String toString() {
        String startStr = start.isPresent() ? String.valueOf(start.get()) : "";
        String stopStr  = stop.isPresent() ? String.valueOf(stop.get())   : "";
        String stepStr  = step.isPresent() ? String.valueOf(step.get())   : "";
        return startStr + ":" + stopStr + ":" + stepStr;
    }
}


/*
class SliceSelectorNode(SelectorNode):
    def __init__(self, token: Token, start: int | None, end: int | None, step: int | None) -> None:
        """Token is last token in series of tokens that make up the slice selector, either a colon or INT"""
        super().__init__(token, ASTNodeType.SLICE_SELECTOR)
        if start:
            if start < JPathBNFConstants.INT_MIN:
                raise IndexError("Start value is less than the minimum allowed value.")
            if start > JPathBNFConstants.INT_MAX:
                raise IndexError("Start value is greater than the maximum allowed value.")
        if end:
            if end < JPathBNFConstants.INT_MIN:
                raise IndexError("End value is less than the minimum allowed value.")
            if end > JPathBNFConstants.INT_MAX:
                raise IndexError("End value is greater than the maximum allowed value.")
        if step:
            if step < JPathBNFConstants.INT_MIN:
                raise IndexError("Step value is less than the minimum allowed value.")
            if step > JPathBNFConstants.INT_MAX:
                raise IndexError("Step value is greater than the maximum allowed value.")

        self._start: int | None = start
        self._end: int | None   = end
        self._step: int | None  = step

    @property
    def slice_op(self) -> slice:
        """Return a slice object from the node's state. """
        return slice(self._start, self._end, self._step)

    def __repr__(self) -> str:
        repr_str = f"SliceSelectorNode(start={repr(self._start)}, end={repr(self._end)}, step={repr(self._step)})"
        return repr_str

    def __str__(self) -> str:
        start_str = self._start if self._start is not None else ''
        end_str   = self._end   if self._end   is not None else ''
        step_str  = self._step  if self._step  is not None else ''

        repr_str = f"{start_str}:{end_str}:{step_str}"
        return f"{self._node_type.abbreviation}({repr_str})"
 */