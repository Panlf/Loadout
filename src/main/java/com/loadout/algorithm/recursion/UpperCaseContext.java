package com.loadout.algorithm.recursion;

/**
 * 传入T,R
 * @author  panlf
 * @date  2020/06/05
 */
@FunctionalInterface
public interface UpperCaseContext<T,U>  {
    String dealContext(String t,String u);
}
