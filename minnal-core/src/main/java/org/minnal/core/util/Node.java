/**
 * 
 */
package org.minnal.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author ganeshs
 *
 */
public abstract class Node<T extends Node<T, P>, P extends Node<T, P>.NodePath> {

	private T parent;
	
	private LinkedList<T> children = new LinkedList<T>();
	
	protected abstract T getThis();
	
	public T addChild(T child) {
		return addChild(child, false);
	}
	
	public T addChild(T child, boolean first) {
		child.parent = getThis();
		if (first) {
			children.addFirst(child);
		} else {
			children.addLast(child);
		}
		return child;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	/**
	 * Does a DFT starting from this node. On every node visit calls the visitor with the node being visited
	 * 
	 * @param visitor
	 */
	public void traverse(Visitor<T> visitor) {
		traverse(getThis(), visitor);
	}
	
	/**
	 * Recursive implementation of DFT. On every node visit calls the visitor with the node being visited
	 * 
	 * @param root
	 * @param visitor
	 */
	private void traverse(T root, Visitor<T> visitor) {
		for (T child : root.children) {
			traverse(child, visitor);
		}
		visitor.visit(root);
	}
	
	public void traverse(PathVisitor<P, T> visitor) {
		LinkedList<T> list = new LinkedList<T>();
		Stack<T> stack = new Stack<T>();
		stack.add(getThis());
		T node = null;
		P path = null;
		while(! stack.isEmpty()) {
			node = stack.pop();
			list.add(node);
			path = createNodePath(copy(list));
			visitor.visit(path);
			if (node.hasChildren()) {
				for (T child : node.children) {
					stack.push(child);
				}
			} else {
				list.removeLast();
			}
		}
	}
	
	protected abstract P createNodePath(List<T> path);
	
	/**
	 * @return the parent
	 */
	public T getParent() {
		return parent;
	}

	/**
	 * @return the children
	 */
	public LinkedList<T> getChildren() {
		return children;
	}

	private List<T> copy(List<T> list) {
		return new ArrayList<T>(list);
	}
	
	public interface Visitor<T> {
	
		void visit(T node);
	}
	
	public interface PathVisitor<P, T> {
		
		void visit(P path);
	}
	
	public class NodePath implements Iterable<T> {
		
		private List<T> path;
		
		public NodePath(List<T> path) {
			this.path = path;
		}

		public Iterator<T> iterator() {
			return path.iterator();
		}
		
		public int size() {
			return path.size();
		}
	
		public T get(int index) {
			return path.get(index);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodePath other = (NodePath) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

		private Node getOuterType() {
			return Node.this;
		}
	}
	
}