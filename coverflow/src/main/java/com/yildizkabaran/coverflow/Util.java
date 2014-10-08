package com.yildizkabaran.coverflow;

public class Util {

  private Util(){  
  }
  
  public static int positiveMod(int a, int b){
    return ((a % b) + b) % b;
  }
}
