package svegon.utils.interfaces;

/**
 * Marks its subclasses as solely comparable by identity.
 * This includes but doesn't limit to:
 * equals(Object o) only returns true if this == o
 * hashCode() returns its identity hash code
 * compareTo(Object o) returns 0 only if this == o
 */
public interface IdentityComparable {
}
