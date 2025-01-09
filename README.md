SELECT sr_no, sold_to_address_role, sold_to_address_id, network_site_address_role, network_site_address_id, additional_partner_address_role, additional_partner_address_id
	FROM public.other_address;

SELECT sr_no, state_or_province, ship_to_address_id, ship_to_address_role
	FROM public.ship_to_address;
