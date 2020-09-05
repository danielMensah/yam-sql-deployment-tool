CREATE OR REPLACE FUNCTION get_test_users(i_user_id integer) RETURNS integer
    STABLE
    LANGUAGE plpgsql
AS
$$
BEGIN

    RETURN 5;

END;
$$;