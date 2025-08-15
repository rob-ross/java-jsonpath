package org.killeroonie.jsonpath;


public class NodeList { }

/*
"""List of JSONPathMatch objects, analogous to the spec's nodelist."""

def values(self) -> List[object]:
        """Return the values from this node list."""
        return [match.obj for match in self]

def values_or_singular(self) -> object:
        """Return the values from this node list."""
        if len(self) == 1:
        return self[0].obj
        return [match.obj for match in self]

def paths(self) -> List[str]:
        """Return a normalized path for each node in this node list."""
        return [match.path for match in self]

def empty(self) -> bool:
        """Return `True` if this node list is empty."""
        return not bool(self)

def __str__(self) -> str:
        return f"NodeList{super().__str__()}"

*/

