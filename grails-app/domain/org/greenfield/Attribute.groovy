package org.greenfield

class Attribute {

	String name

	Date dateCreated
	Date lastUpdated
	
	
	static mapping = {
		sort name: "asc"
	}
	
	static constraints = {
		name(nullable:false, unique:true)
		id generator: 'sequence', params:[sequence:'ID_FILTER_ATTRIBUTE_PK_SEQ']
    }
}
