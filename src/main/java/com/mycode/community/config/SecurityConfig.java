package com.mycode.community.config;

import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    // 重写父类的三个方法


    // 忽略对所有静态资源访问的请求
    @Override
    public void configure(WebSecurity web) throws Exception {
        // super.configure(web);
        // security忽略静态资源的访问，提高性能
        web.ignoring().antMatchers("/resources/**");
    }

//    // 处理认证
//    //    该方法涉及的核心组件：
//    //    AuthenticationManager:认证的核心接口
//    //    AuthenticationManagerBuilder:用于构建AuthenticationManager的工具类
//    //    ProviderManager:AuthenticationManager接口的默认实现类
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        // 只需要提供配置即可
//
//        // 内置的认证规则
//        // 对密码进行加密，new Pbkdf2PasswordEncoder("12345")相当于salt增加密码难度
//        // auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("12345"));
//
//        // 但以上与当前系统设计不匹配
//        // 使用如下：自定义认证规则
//        // AuthenticationProvider: ProviderManager持有一组AuthenticationProvider，每个AuthenticationProvider负责一种认证
//        // ProviderManager不直接去做认证，让里面的一组组件AuthenticationProvider去实现这些认证
//        // 这种形式的设计模式为：委托模式-ProviderManager将认证委托给AuthenticationProvider
//        auth.authenticationProvider(new AuthenticationProvider() {
//
//            // Authentication:用于封装认证信息（例如：账号、密码）的接口，不同的实现类代表不同的认证信息
//            @Override
//            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//
                    // 此处进行处理认证数据
//
//                // 返回认证结果：AuthenticationProvider的实例-哪种类型的认证？
//                // UsernamePasswordAuthenticationToken(认证结果主信息(通常：用户实体信息), 当前用户登陆凭证, 当前用户权限)
//                // principal：主要信息， credentials：证书， authorities：权限
//                return  new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
//            }
//
//            // 当前的AuthenticationProvider支持哪种类型的认证（账号密码、指纹、第三方登录等等）
//            @Override
//            public boolean supports(Class<?> aClass) {
//                // UsernamePasswordAuthenticationToken: Authentication接口常用的实现类
//                return UsernamePasswordAuthenticationToken.class.equals(aClass);
//            }
//
//            // 当进行登陆时，security能够捕获、拦截登陆请求，确认该请求是登陆请求后，
//            // security会调用该接口：AuthenticationProvider，调用authenticate方法，
//            // 认证结果传入UsernamePasswordAuthenticationToken对象中，该结果和对象在授权时会被使用
//        });

//    }

    /**
     *  由于项目已经完成了登录退出方面的认证过程，所以：
     *      不对configure(AuthenticationManagerBuilder auth)认证流程进行重写
     *      需要绕过Security认证流程，采用原来的系统认证方案
     */


    // 授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 引用security后，引用父类的configure方法，对任何请求都进行验证授权
        // 父类源码：authorizeRequests().anyRequest()).authenticated()

        // super.configure(http);

        // 授权相关路径
        http.authorizeRequests()
                // 表示该路径下持有有哪些权限才可以访问，hasAnyAuthority：有任何一种角色权限都可访问
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                // 版主特有权限
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                // 管理员特有
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                // 除了以上的限制，其他的任何请求都允许访问
                .anyRequest().permitAll()
                // 关闭security开启的csrf，也可开启，要对，每个异步请求时添加token
                .and().csrf().disable();

        // 权限不够时的处理
        // security底层捕获到用户没有权限时的异常时的处理
        http.exceptionHandling()
                // 没有登录时如何处理？
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登录
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 参数：request、response、异常

                        // 获取请求是何种类型的请求
                        String xRequestWith = request.getHeader("X-requested-with");
                        if ("XMLHttpRequest".equals(xRequestWith)) {
                            // 异步请求
                            // 给浏览器响应，响应一串json字符串
                            response.setContentType("application/plain; charset=utf-8");
                            // 通过字符流向前台输出
                            PrintWriter writer = response.getWriter();
                            // 当没有权限时，服务器拒绝你的访问请求，而不是服务器报错时，通常返回403
                            writer.write(CommunityUtil.getJSONString(403, "您还没有登录！"));

                        } else {
                            // 其他请求
                            response.sendRedirect(request.getContextPath() + "/login");
                        }

                    }
                })
                // 登录了，但是用户权限不够访问某链接时如何处理？
                //.accessDeniedPage("xxx"),这种方式也可，但在正式的项目中，处理过于简陋，可能请求会有普通请求和异步请求等
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        // 同样需要判断请求类型，是返回json还是返回html

                        // 获取请求是何种类型的请求
                        String xRequestWith = request.getHeader("X-requested-with");
                        if ("XMLHttpRequest".equals(xRequestWith)) {
                            // 异步请求
                            // 给浏览器响应，响应一串json字符串
                            response.setContentType("application/plain; charset=utf-8");
                            // 通过字符流向前台输出
                            PrintWriter writer = response.getWriter();
                            // 当没有权限时，服务器拒绝你的访问请求，而不是服务器报错时，通常返回403
                            writer.write(CommunityUtil.getJSONString(403, "您没有访问此功能的权限！"));

                        } else {
                            // 其他请求
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // 默认情况下，security会自动拦截名为logout的请求路径，就会自动处理
        // 众所周知，security底层都是通过filter来做权限管理，filter的执行在dispatcherServlet之前
        // 所以他在Controller之前拦截到了logout请求之后进行处理，程序就不会往下走，后面的自己写的logout就不会执行

        // 绕过security对logout请求的处理
        // 覆盖它默认的逻辑，才能执行我们自己的代码
        http.logout()
                // 对默认处理的路径进行修改：/securityLogout，而我们自己使用/logout
                .logoutUrl("/securityLogout");


    }

    /**
     *  我们做了授权但是没有做认证，绕开了security的认证流程，选择使用自己的登录流程
     *
     *      原本：security认证完成之后，会将认证完的信息封装到一个token中-UsernamePasswordAuthenticationToken，
     *      这个token会被security的Filter获取到，而这个Filter会将这个token传给SecurityContext中，
     *    后面进行授权时：
     *      security都是通过该token来判断你的权限
     *    而此时我们是进行自己的认证，没有该token，security是不知道这个token的，无法帮我们进行授权
     *    我们需要将我们的token存到SecurityContext中
     *
     *    1、封装获取用户权限的方法：getAuthorities
     *    2、在拦截器LoginTicketInterceptor中判断了用户的登录凭证，是否登录
     *       所以说，在拦截器中，用户每次登录，服务器会给用户传一个ticket，让用户存储，用户每次访问是，都传给服务器该ticket，
     *       服务器通过该ticket判断ticket是否正确，是否过期，是否登录
     *
     *       在该拦截器当中，即可判断用户是否登录，登录是否有效，如果登录有效，意味着是有权限的用户，就可以查询用户的权限，
     *       再去存储到securityContext对象中
     *
     */
}
