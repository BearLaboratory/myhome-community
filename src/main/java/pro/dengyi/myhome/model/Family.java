package pro.dengyi.myhome.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author BLab
 */
@Data
@TableName("family")
@ApiModel("家庭")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Family {

    @ApiModelProperty(value = "家庭Id")
    private String id;

    @ApiModelProperty(value = "家庭名")
    @NotBlank
    @Length(min = 2, max = 10)
    private String name;

}