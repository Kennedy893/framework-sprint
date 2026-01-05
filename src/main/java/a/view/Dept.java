package a.view;

public class Dept 
{
    public Dept(String deptName, String deptCode) {
        this.deptName = deptName;
        this.deptCode = deptCode;
    }
    public Dept() {
    }
    private String deptName;
    private String deptCode;
    public String getDeptName() {
        return deptName;
    }
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    public String getDeptCode() {
        return deptCode;
    }
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
}
