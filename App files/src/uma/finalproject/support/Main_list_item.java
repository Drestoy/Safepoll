/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.support;

public class Main_list_item {

	private String groupName;
	private String picture;
	private String bbdd_ID;
	private String desc;
	private String options;
	
	public Main_list_item(String name, String pic, String id, String description, String options){
		groupName = name;
		picture = pic;
		bbdd_ID = id;
		desc = description;
		this.options = options;
	}
	
	public String getName(){
		return groupName;
	}
	
	public String getImage(){
		return picture;
	}
	
	public String getID(){
		return bbdd_ID;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public String getOptions(){
		return options;
	}
}
