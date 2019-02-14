梁飞的博客https://javatar.iteye.com/blog/690845

dubbo框架为了兼容更多的需求，在不停的拓展，常用的拓展方式是将新旧功能拓展成一个通用的实现，有些情况下也可以考虑使用增量式拓展

举例：

- 序列化

  序列化就是将流转换成对象，将对象转换成流。但是有些地方会使用osgi，这样的话，IO所在的classLoader可能和业务的classLoader是隔离的，需要将流转成byte[]，
  然后传给业务方classLoader进行序列化。为了适应这个场景，将非osgi和osgi进行了拓展。这样，不管是什么场景都需要将流转成byte[]，但是大部分的场景都是不需要osgi的。
  如果采用增量式拓展，非osgi的代码不动，再加一个osgi的实现，要用的时候直接依赖osgi实现即可

- 接口方法的远程调用

  最开始远程方法都是基于接口的，这样，拓展接口就是invoke(Method method, Objects[] args).后来，有了无接口调用，就是没有接口方法也能调用，并将对象转出Map表示，
  因为Method对象是不能直接new出来的，我们就拓展为invoke(String methodName, String[] parameterTypes, String retureTypes, Object[] args),导致不管是不是无接口调用都需要转换。
  采用增量拓展的话，增加一个GeneralService接口，里面有一个通用的invoke方法，会将接收到的调用转给目标接口

- 消息发送 
  