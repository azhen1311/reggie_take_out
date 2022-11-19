package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //将密码加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//        根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryChainWrapper = new LambdaQueryWrapper<>();
        queryChainWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryChainWrapper);

//        如果没有查询到返回登录失败
        if (emp == null) {
            return R.error("登录失败");
        }

//        密码比对
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

//        查看员工状态
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

//        登录成功，返回成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }


    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {

        //清除Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");

        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {

        //设置初始加密
        employee.setPassword(DigestUtils.md5DigestAsHex("12345".getBytes()));

/*
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获取当前用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
*/

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> pageR(int page, int pageSize, String name) {

        //构造分页构造器
        Page pageInfo = new Page<>(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);

    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

/*        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);
        employee.setUpdateTime(LocalDateTime.now());*/

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getByid(@PathVariable Long id) {
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        if (employee != null) {

            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }

}
